package lat.pam.ydjob.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import lat.pam.ydjob.R
import lat.pam.ydjob.databinding.ItemCompanyJobBinding
import lat.pam.ydjob.model.Job
import java.text.SimpleDateFormat
import java.util.Locale

class CompanyJobAdapter(
    private val jobList: List<Job>,
    private val onViewApplicants: (Job) -> Unit
) : RecyclerView.Adapter<CompanyJobAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCompanyJobBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCompanyJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = jobList[position]

        holder.binding.tvJobTitle.text = job.title

        val date = job.createdAt
        if (date != null) {
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            holder.binding.tvPostDate.text = "Posted on ${format.format(date)}"
        } else {
            holder.binding.tvPostDate.text = "Posted recently"
        }

        holder.binding.tvApplicantCount.text = "Check Applicants"

        if (job.companyLogo.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(job.companyLogo)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.binding.ivJobIcon)
        } else {
            holder.binding.ivJobIcon.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.binding.btnViewApplicants.setOnClickListener {
            onViewApplicants(job)
        }

        holder.itemView.setOnClickListener {
            onViewApplicants(job)
        }
    }

    override fun getItemCount() = jobList.size
}